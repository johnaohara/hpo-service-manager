import * as React from 'react';
import {
    Breadcrumb,
    BreadcrumbItem,
    PageSection,
    Card,
    CardHeader,
    CardBody, Button, Split, SplitItem, Stack, StackItem
} from '@patternfly/react-core';
import {ExperimentsTable} from './components/ExperimentsTable'
import {useHistory} from "react-router-dom";


const Experiments = () => (
  <PageSection>
      <Stack hasGutter={true}>
          <StackItem><ExperimentsHeader/></StackItem>
          <StackItem isFilled><ExperimentsTable/></StackItem>
      </Stack>
  </PageSection>
)

function ExperimentsHeader() {
    const history = useHistory();

    function newExperiment() {
        history.push("/new");
    }

    return (
        <Card>
            <CardHeader> <Breadcrumb>
                <BreadcrumbItem to="#" isActive>experiments</BreadcrumbItem>
            </Breadcrumb>
            </CardHeader>

            <CardBody>

                <Split>
                    <SplitItem>Running Experiments</SplitItem>
                    <SplitItem isFilled>&nbsp;</SplitItem>
                    <SplitItem><Button onClick={newExperiment}>New</Button></SplitItem>
                </Split>


            </CardBody>
        </Card>
    )
}


export { Experiments };
