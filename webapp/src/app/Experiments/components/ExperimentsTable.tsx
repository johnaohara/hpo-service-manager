import React from 'react';
import {
    Progress,
    ProgressSize,
    ProgressMeasureLocation,
    ActionList,
    ActionListItem,
    Button,
    Dropdown,
    DropdownItem,
    DropdownSeparator,
    KebabToggle, Bullseye, EmptyState, EmptyStateVariant, EmptyStateIcon, EmptyStateBody, Title, ProgressVariant, Modal
} from '@patternfly/react-core';
import {
    TableComposable,
    Thead,
    Tr,
    Th,
    Tbody,
    Td
} from '@patternfly/react-table';

import {Link, Route} from "react-router-dom";
import {SearchIcon} from "@patternfly/react-icons";
import {ExperimentDetails} from "@app/Experiments/components/ExperiementDetails";


type ExampleType = 'default' | 'compact' | 'compactBorderless';


interface IExperimentState {
    name: string;
    state: string;
}

interface IExperiment {
    experimentName: string;
    total_Trials: number;
    currentTrial: number;
    currentState: string;
}

export function ExperimentsTable() {

    const [experiments, setExperiments] = React.useState<IExperiment[]>([]);

    const columnNames = {
        name: 'Name',
        trial: 'Progress',
        actions: 'Actions'
    };

    let newExpRequest = new Request(
        "/api/hpo/experiment",
        {
            method: "get",
        }
    )

    fetch(newExpRequest)
        .then(res => res.json())
        .then(res => {
            setExperiments(res);
        })


    return (
        <TableComposable
            // aria-label="Simple table"
            variant='compact'
            borders={true}
            aria-label="Empty state table"
        >
            <Thead>
                <Tr>
                    <Th width={20}>{columnNames.name}</Th>
                    <Th width={70}>{columnNames.trial}</Th>
                    <Th width={10}>{columnNames.actions}</Th>
                </Tr>
            </Thead>
            <Tbody>
                <Tr hidden={!(experiments.length === 0)}>
                    <Td colSpan={8}>
                        <Bullseye>
                            <EmptyState variant={EmptyStateVariant.small}>
                                <EmptyStateIcon icon={SearchIcon}/>
                                <Title headingLevel="h2" size="lg">
                                    No experiments found
                                </Title>
                                <EmptyStateBody>Please create a new Experiment and try again</EmptyStateBody>
                            </EmptyState>
                        </Bullseye>
                    </Td>
                </Tr>
                {experiments.map(experiment => (
                    <Tr key={experiment.experimentName}>
                        <Td dataLabel={columnNames.name}><Link
                            to={'/experiment/' + experiment.experimentName}>{experiment.experimentName}</Link></Td>
                        <Td dataLabel={columnNames.trial}>
                            <Progress
                                measureLocation={ProgressMeasureLocation.outside}
                                value={experiment.currentTrial / experiment.total_Trials * 100.0}
                                size={ProgressSize.md}
                                variant={experiment.currentState === 'RUNNING' ? ProgressVariant.success : ProgressVariant.warning}

                            /></Td>
                        <Td dataLabel={columnNames.actions}
                            isActionCell={true}><ActionListSingleGroup experimentName={experiment.experimentName}/></Td>
                    </Tr>
                ))}
            </Tbody>
        </TableComposable>
    );
}


const ActionListSingleGroup = ({experimentName}) => {
    const [isOpen, setIsOpen] = React.useState(false);

    const onToggle = (
        isOpen: boolean,
        event: MouseEvent | TouchEvent | KeyboardEvent | React.KeyboardEvent<any> | React.MouseEvent<HTMLButtonElement>
    ) => {
        event.stopPropagation();
        setIsOpen(isOpen);
    };

    const onSelect = (event: React.SyntheticEvent<HTMLDivElement, Event>) => {
        event.stopPropagation();
        setIsOpen(!isOpen);
    };


    const startRun = async () => {
        // alert ('start: ' + experimentName)
        let state = '{"name": "' + experimentName + '", "state": "RUNNING"};'

        let uppdateExpRequest = new Request(
            "/api/hpo/experiment/state",
            {
                method: "put",
                headers: {'Content-Type': 'application/json'},
                body: state
            }
        )

        let response = await fetch(uppdateExpRequest);

        if (response.ok) {
            let data = response.json();
            // console.log(data);
        } else {
            let data = await response.json();
        }

    }

    const deleteExperiment = async () => {
        if( confirm('Are you sure you want to delete experiment: ' + experimentName + "?") ){
            let uppdateExpRequest = new Request(
                "/api/hpo/experiment/" + experimentName,
                {
                    method: "delete"
                }
            )

            let response = await fetch(uppdateExpRequest);

            if (response.ok) {
            } else {
                let data = await response.json();
            }
        }

    }

    const deleteModal = function () {
        const [isModalOpen, setIsOpen] = React.useState(false);

        const handleModalToggle = () => {
            setIsOpen(!isModalOpen);
        };
        return (<Modal
                title="Simple modal header"
                isOpen={isModalOpen}
                onClose={handleModalToggle}
                actions={[
                    <Button key="confirm" variant="primary" onClick={handleModalToggle}>
                        Confirm
                    </Button>,
                    <Button key="cancel" variant="link" onClick={handleModalToggle}>
                        Cancel
                    </Button>
                ]}
            >
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et
                dolore
                magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea
                commodo
                consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
                pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim
                id
                est laborum.
            </Modal>
        )


    }


    const pauseRun = async () => {
        // alert ('start: ' + experimentName)
        let state = '{"name": "' + experimentName + '", "state": "PAUSED"};'

        let uppdateExpRequest = new Request(
            "/api/hpo/experiment/state",
            {
                method: "put",
                headers: {'Content-Type': 'application/json'},
                body: state
            }
        )

        let response = await fetch(uppdateExpRequest);

        if (response.ok) {
            let data = response.json();
            // console.log(data);
        } else {
            let data = await response.json();
        }

    }

    const dropdownItems = [
        <DropdownItem key="run action" component="button" onClick={startRun}>
            Run
        </DropdownItem>,
        <DropdownItem key="pause action" component="button" onClick={pauseRun}>
            Pause
        </DropdownItem>,
        <DropdownSeparator key="separator"/>,
        <DropdownItem key="separated link">
            <Link to={'/experiment/' + experimentName}>Details</Link>
        </DropdownItem>,
        <DropdownSeparator key="separator"/>,
        <DropdownItem key="cancel action" component="button" onClick={deleteExperiment}>
            Delete
        </DropdownItem>,
        // <DropdownItem key="separated link">Edit</DropdownItem>
    ];

    return (
        <React.Fragment>
            <ActionList>
                <ActionListItem>
                    <Dropdown
                        // onSelect={onSelect}
                        toggle={<KebabToggle onToggle={onToggle}/>}
                        isOpen={isOpen}
                        isPlain
                        dropdownItems={dropdownItems}
                        position="right"
                    />
                </ActionListItem>
            </ActionList>
        </React.Fragment>
    );
};
