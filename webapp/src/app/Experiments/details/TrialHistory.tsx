import React from 'react';
import {
    Chart,
    ChartAxis,
    ChartGroup,
    ChartLine,
    ChartScatter,
    ChartThemeColor,
    ChartVoronoiContainer,
    getResizeObserver
} from '@patternfly/react-charts';
import {Title} from "@patternfly/react-core";

export const TrialHistory: React.FunctionComponent = () => {

    const series = [
        {
            datapoints: [
                {name: 'Trials', x: '49', y: 36.15},
                {name: 'Trials', x: '50', y: 55.14},
                {name: 'Trials', x: '51', y: 86.49},
                {name: 'Trials', x: '52', y: 23.17},
                {name: 'Trials', x: '53', y: 82.14},
                {name: 'Trials', x: '54', y: 84.23},
                {name: 'Trials', x: '55', y: 68.16},
                {name: 'Trials', x: '56', y: 73.29},
                {name: 'Trials', x: '57', y: 90.0},
                {name: 'Trials', x: '58', y: 37.5}
            ],
            legendItem: {name: 'Trials'}
        },
    ];

    return (
            <div style={{height: '275px'}}>
                <Title headingLevel={"h2"}>Trial History</Title>
                <Chart
                    ariaDesc="Average number of pets"
                    ariaTitle="Line chart example"
                    // containerComponent={
                    //     <ChartVoronoiContainer
                    //         labels={({datum}) => datum.childName.includes('line-') ? `${datum.name}: ${datum.y}` : null}
                    //         constrainToVisibleArea
                    //     />
                    // }
                    legendData={series.map(s => s.legendItem)}
                    legendPosition="bottom-left"
                    height={275}
                    maxDomain={{y: 100}}
                    minDomain={{y: 0}}
                    padding={{
                        bottom: 75, // Adjusted to accommodate legend
                        left: 50,
                        right: 50,
                        top: 50
                    }}
                    themeColor={ChartThemeColor.orange}
                    width={1400}
                >
                    <ChartAxis tickValues={[20, 30, 40]}/>
                    <ChartAxis dependentAxis showGrid tickValues={[20, 50, 80]}/>
                    <ChartGroup>
                        {series.map((s, idx) => {
                            return (
                                <ChartScatter
                                    data={s.datapoints}
                                    key={'scatter-' + idx}
                                    name={'scatter-' + idx}
                                />
                            );
                        })}
                    </ChartGroup>
                    <ChartGroup>
                        {series.map((s, idx) => {
                            return (
                                <ChartLine
                                    key={'line-' + idx}
                                    name={'line-' + idx}
                                    data={s.datapoints}
                                />
                            );
                        })}
                    </ChartGroup>
                </Chart>
            </div>
    );
}